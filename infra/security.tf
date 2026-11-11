# ---------- Bouncers ----------

resource "aws_security_group" "alb" {
  name        = "${var.project}-alb"
  description = "Load balancer: open to the internet on port 80"
  vpc_id      = aws_vpc.main.id
}

resource "aws_security_group" "app" {
  name        = "${var.project}-app"
  description = "App tasks: only the load balancer may connect"
  vpc_id      = aws_vpc.main.id
}

resource "aws_security_group" "db" {
  name        = "${var.project}-db"
  description = "Postgres: only the app may connect"
  vpc_id      = aws_vpc.main.id
}

resource "aws_security_group" "redis" {
  name        = "${var.project}-redis"
  description = "Redis: only the app may connect"
  vpc_id      = aws_vpc.main.id
}

# ---------- Guest lists ----------

# ALB: anyone on the internet, port 80
resource "aws_vpc_security_group_ingress_rule" "alb_http" {
  security_group_id = aws_security_group.alb.id
  cidr_ipv4         = "0.0.0.0/0"
  from_port         = 80
  to_port           = 80
  ip_protocol       = "tcp"
}

# ALB may send to anything (it forwards to the app)
resource "aws_vpc_security_group_egress_rule" "alb_out" {
  security_group_id = aws_security_group.alb.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}

# App: only from the ALB, port 8080
resource "aws_vpc_security_group_ingress_rule" "app_from_alb" {
  security_group_id            = aws_security_group.app.id
  referenced_security_group_id = aws_security_group.alb.id
  from_port                    = 8080
  to_port                      = 8080
  ip_protocol                  = "tcp"
}

# App may reach out anywhere (ECR, SQS, CloudWatch, Postgres, Redis)
resource "aws_vpc_security_group_egress_rule" "app_out" {
  security_group_id = aws_security_group.app.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}

# Postgres: only from the app, port 5432
resource "aws_vpc_security_group_ingress_rule" "db_from_app" {
  security_group_id            = aws_security_group.db.id
  referenced_security_group_id = aws_security_group.app.id
  from_port                    = 5432
  to_port                      = 5432
  ip_protocol                  = "tcp"
}

# Redis: only from the app, port 6379
resource "aws_vpc_security_group_ingress_rule" "redis_from_app" {
  security_group_id            = aws_security_group.redis.id
  referenced_security_group_id = aws_security_group.app.id
  from_port                    = 6379
  to_port                      = 6379
  ip_protocol                  = "tcp"
}