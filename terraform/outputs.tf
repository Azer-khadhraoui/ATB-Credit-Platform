# Outputs surface the values worth knowing after an apply, without digging through the state.
# They are also how one module hands information to another once the setup grows.
output "namespace" {
  description = "Namespace Terraform manages"
  value       = kubernetes_namespace.atb.metadata[0].name
}

output "ml_service_endpoint" {
  description = "In-cluster address other services would use to reach the ML service"
  value       = "http://${kubernetes_service.ml_service.metadata[0].name}.${kubernetes_namespace.atb.metadata[0].name}.svc.cluster.local:8000"
}
