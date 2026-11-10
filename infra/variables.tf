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