resource "aws_elasticache_subnet_group" "main" {
  name       = "${var.project}-cache"
  subnet_ids = aws_subnet.private[*].id
}

resource "aws_elasticache_replication_group" "main" {
  replication_group_id = "${var.project}-cache"
  description          = "URL shortener cache"

  engine         = "valkey"
  engine_version = "8.0"
  node_type      = "cache.t4g.micro"
  port           = 6379

  num_cache_clusters         = 1     # one node to save credits; production: 2+
  automatic_failover_enabled = false # needs 2+ nodes

  subnet_group_name  = aws_elasticache_subnet_group.main.name
  security_group_ids = [aws_security_group.redis.id]

  at_rest_encryption_enabled = true
  transit_encryption_enabled = true # the app must connect with TLS

  apply_immediately = true
}