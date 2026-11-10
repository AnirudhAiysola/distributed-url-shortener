# The private shelf for our Docker images
resource "aws_ecr_repository" "app" {
  name                 = var.project
  image_tag_mutability = "IMMUTABLE" # a tag can never be overwritten
  force_delete         = true        # let `terraform destroy` remove it even with images inside

  image_scanning_configuration {
    scan_on_push = true # AWS checks each image for known security holes
  }
}

# Keep only the 10 newest images, so old ones don't pile up and cost money
resource "aws_ecr_lifecycle_policy" "app" {
  repository = aws_ecr_repository.app.name

  policy = jsonencode({
    rules = [{
      rulePriority = 1
      description  = "Keep the last 10 images"
      selection = {
        tagStatus   = "any"
        countType   = "imageCountMoreThan"
        countNumber = 10
      }
      action = { type = "expire" }
    }]
  })
}