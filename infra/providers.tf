provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Project   = "url-shortener"
      ManagedBy = "terraform"
    }
  }
}