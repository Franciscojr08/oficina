output "account_id" {
  description = "Conta AWS atual."
  value       = data.aws_caller_identity.current.account_id
}

output "cluster_name" {
  description = "Nome do cluster EKS."
  value       = aws_eks_cluster.this.name
}

output "cluster_endpoint" {
  description = "Endpoint da API do EKS."
  value       = aws_eks_cluster.this.endpoint
}

output "rds_endpoint" {
  description = "Endpoint do RDS PostgreSQL."
  value       = aws_db_instance.postgres.address
}

output "rds_port" {
  description = "Porta do RDS."
  value       = aws_db_instance.postgres.port
}

output "jdbc_url" {
  description = "URL JDBC para a aplicação."
  value       = "jdbc:postgresql://${aws_db_instance.postgres.address}:${aws_db_instance.postgres.port}/${var.db_name}"
}

output "next_commands" {
  description = "Próximos comandos após o apply."
  value       = <<EOT
aws eks update-kubeconfig --region ${var.aws_region} --name ${aws_eks_cluster.this.name} --profile academy
kubectl get nodes
EOT
}
