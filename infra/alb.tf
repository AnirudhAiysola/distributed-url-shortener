# The receptionist: public, on the public streets in both zones
resource "aws_lb" "main" {
  name               = "${var.project}-alb"
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = aws_subnet.public[*].id
}

# The list of healthy API tasks
resource "aws_lb_target_group" "api" {
  name        = "${var.project}-api"
  port        = 8080
  protocol    = "HTTP"
  target_type = "ip" # Fargate tasks are registered by IP
  vpc_id      = aws_vpc.main.id

  deregistration_delay = 30 # during deploys, let in-flight requests finish for 30s

  health_check {
    path                = "/actuator/health"
    matcher             = "200"
    interval            = 15
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
  }
}

# Port 80 → forward everything to the API tasks
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.main.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.api.arn
  }
}