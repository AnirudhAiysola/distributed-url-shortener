output "ecr_repository_url" {
  description = "Where to push Docker images"
  value       = aws_ecr_repository.app.repository_url
}
output "db_endpoint" {
  description = "Postgres host"
  value       = aws_db_instance.main.address
}

output "db_secret_arn" {
  description = "Secrets Manager secret holding the DB username and password"
  value       = aws_db_instance.main.master_user_secret[0].secret_arn
}

output "redis_endpoint" {
  description = "Valkey/Redis host"
  value       = aws_elasticache_replication_group.main.primary_endpoint_address
}

output "click_events_queue_url" {
  value = aws_sqs_queue.click_events.url
}

output "app_url" {
  description = "Public address of the app"
  value       = "http://${aws_lb.main.dns_name}"
}
output "loadgen_instance_id" {
  value = one(aws_instance.loadgen[*].id)
}