variable "region" {
  description = "AWS region to deploy into"
  type        = string
  default     = "us-west-2"
}

variable "project" {
  description = "Name prefix for all resources"
  type        = string
  default     = "url-shortener"
}
variable "image_tag" {
  description = "Docker image tag to deploy (the git commit ID)"
  type        = string
}
variable "loadgen_enabled" {
  description = "Create the k6 load-generator instance"
  type        = bool
  default     = false
}