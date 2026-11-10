output "ecr_repository_url" {
  description = "Where to push Docker images"
  value       = aws_ecr_repository.app.repository_url
}