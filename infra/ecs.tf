resource "aws_ecs_cluster" "main" {
  name = var.project
}

resource "aws_cloudwatch_log_group" "app" {
  name              = "/ecs/${var.project}"
  retention_in_days = 3
}

locals {
  image     = "${aws_ecr_repository.app.repository_url}:${var.image_tag}"
  db_secret = aws_db_instance.main.master_user_secret[0].secret_arn

  # Settings shared by the API and the worker
  common_environment = [
    { name = "SPRING_PROFILES_ACTIVE", value = "aws" },
    { name = "SPRING_DATASOURCE_URL", value = "jdbc:postgresql://${aws_db_instance.main.address}:5432/shortener" },
    { name = "SPRING_DATA_REDIS_HOST", value = aws_elasticache_replication_group.main.primary_endpoint_address },
    { name = "SHORTENER_BASE_URL", value = "http://${aws_lb.main.dns_name}" },
  ]

  # Pulled from Secrets Manager by ECS at startup ("<secret ARN>:<JSON field>::")
  db_secrets = [
    { name = "SPRING_DATASOURCE_USERNAME", valueFrom = "${local.db_secret}:username::" },
    { name = "SPRING_DATASOURCE_PASSWORD", valueFrom = "${local.db_secret}:password::" },
  ]
}

# ---------- Recipe cards ----------

resource "aws_ecs_task_definition" "api" {
  family                   = "${var.project}-api"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = 512  # 0.5 vCPU
  memory                   = 1024 # 1 GB
  execution_role_arn       = aws_iam_role.execution.arn
  task_role_arn            = aws_iam_role.task.arn

  runtime_platform {
    cpu_architecture        = "ARM64" # Graviton: matches our image, and cheaper
    operating_system_family = "LINUX"
  }

  container_definitions = jsonencode([{
    name         = "app"
    image        = local.image
    essential    = true
    portMappings = [{ containerPort = 8080, protocol = "tcp" }]
    environment  = concat(local.common_environment, [{ name = "SHORTENER_WORKER_ENABLED", value = "false" }])
    secrets      = local.db_secrets
    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.app.name
        "awslogs-region"        = var.region
        "awslogs-stream-prefix" = "api"
      }
    }
  }])
}

resource "aws_ecs_task_definition" "worker" {
  family                   = "${var.project}-worker"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = 512
  memory                   = 1024
  execution_role_arn       = aws_iam_role.execution.arn
  task_role_arn            = aws_iam_role.task.arn

  runtime_platform {
    cpu_architecture        = "ARM64"
    operating_system_family = "LINUX"
  }

  container_definitions = jsonencode([{
    name        = "app"
    image       = local.image
    essential   = true
    environment = concat(local.common_environment, [{ name = "SHORTENER_WORKER_ENABLED", value = "true" }])
    secrets     = local.db_secrets
    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.app.name
        "awslogs-region"        = var.region
        "awslogs-stream-prefix" = "worker"
      }
    }
  }])
}

# ---------- Managers ----------

resource "aws_ecs_service" "api" {
  name            = "api"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.api.arn
  desired_count   = 2 # one per zone, so losing a zone doesn't take us down
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.public[*].id
    security_groups  = [aws_security_group.app.id]
    assign_public_ip = true # needed to reach ECR/SQS/CloudWatch without a NAT gateway
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.api.arn
    container_name   = "app"
    container_port   = 8080
  }

  health_check_grace_period_seconds = 90 # give Spring Boot time to start before judging it

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  depends_on = [aws_lb_listener.http]
}

resource "aws_ecs_service" "worker" {
  name            = "worker"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.worker.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.public[*].id
    security_groups  = [aws_security_group.app.id]
    assign_public_ip = true
  }

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }
}