output "cluster_name" {
  description = "Nome do cluster kind criado."
  value       = kind_cluster.this.name
}

output "kubectl_context" {
  description = "Contexto kubectl gerado pelo kind."
  value       = "kind-${kind_cluster.this.name}"
}

output "next_commands" {
  description = "Comandos seguintes para carregar a imagem local e aplicar os manifests."
  value = <<EOT
kubectl cluster-info --context kind-${kind_cluster.this.name}
docker build -t oficina-api:local .
kind load docker-image oficina-api:local --name ${kind_cluster.this.name}
kubectl apply -f k8s
kubectl get pods -n oficina
kubectl port-forward svc/oficina-api 8080:8080 -n oficina
EOT
}
