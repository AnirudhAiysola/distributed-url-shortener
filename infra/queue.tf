# Where messages go after failing 5 times
resource "aws_sqs_queue" "click_events_dlq" {
  name                      = "click-events-dlq"
  message_retention_seconds = 1209600 # 14 days, time to investigate failures
}

resource "aws_sqs_queue" "click_events" {
  name                       = "click-events"
  visibility_timeout_seconds = 30 # hidden this long after being received
  receive_wait_time_seconds  = 20 # long polling

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.click_events_dlq.arn
    maxReceiveCount     = 5
  })
}