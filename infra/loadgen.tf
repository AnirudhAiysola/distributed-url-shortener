# Latest Amazon Linux 2023 image (x86), looked up from AWS's public parameter
data "aws_ssm_parameter" "al2023" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64"
}

# Badge for the instance: allows Session Manager, nothing else
resource "aws_iam_role" "loadgen" {
  count = var.loadgen_enabled ? 1 : 0
  name  = "${var.project}-loadgen"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Action    = "sts:AssumeRole"
      Principal = { Service = "ec2.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "loadgen_ssm" {
  count      = var.loadgen_enabled ? 1 : 0
  role       = aws_iam_role.loadgen[0].name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "loadgen" {
  count = var.loadgen_enabled ? 1 : 0
  name  = "${var.project}-loadgen"
  role  = aws_iam_role.loadgen[0].name
}

# No inbound rules at all: we get in through Session Manager, not SSH
resource "aws_security_group" "loadgen" {
  count       = var.loadgen_enabled ? 1 : 0
  name        = "${var.project}-loadgen"
  description = "k6 load generator: outbound only"
  vpc_id      = aws_vpc.main.id
}

resource "aws_vpc_security_group_egress_rule" "loadgen_out" {
  count             = var.loadgen_enabled ? 1 : 0
  security_group_id = aws_security_group.loadgen[0].id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}

resource "aws_instance" "loadgen" {
  count                  = var.loadgen_enabled ? 1 : 0
  ami                    = data.aws_ssm_parameter.al2023.value
  instance_type          = "c6i.large"
  subnet_id              = aws_subnet.public[0].id
  vpc_security_group_ids = [aws_security_group.loadgen[0].id]
  iam_instance_profile   = aws_iam_instance_profile.loadgen[0].name

  # Runs once at first boot: install k6, copy the script, remember the app URL
  user_data = <<-EOT
    #!/bin/bash
    set -e
    dnf install -y https://dl.k6.io/rpm/repo.rpm
    dnf install -y k6
    cat > /opt/redirect-test.js <<'SCRIPT'
    ${file("${path.module}/../load/redirect-test.js")}
    SCRIPT
    echo 'export BASE_URL=http://${aws_lb.main.dns_name}' > /etc/profile.d/loadtest.sh
  EOT

  tags = { Name = "${var.project}-loadgen" }
}