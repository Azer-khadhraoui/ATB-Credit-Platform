# The same ml-service as k8s/20-ml-service.yaml, expressed in HCL instead of YAML.
# Deploying it here shows the difference in tooling on a workload already understood,
# rather than adding a new one on top.

resource "kubernetes_deployment" "ml_service" {
  metadata {
    name      = "ml-service"
    namespace = kubernetes_namespace.atb.metadata[0].name
    labels    = local.ml_labels
  }

  spec {
    replicas = 1

    selector {
      match_labels = local.ml_labels
    }

    template {
      metadata {
        labels = local.ml_labels
      }

      spec {
        container {
          name = "ml-service"
          # Built locally by `docker compose build`; the cluster shares the daemon's
          # image store, so nothing is pulled from a registry.
          image             = "atb-credit-platform-ml-service:latest"
          image_pull_policy = "IfNotPresent"

          port {
            container_port = 8000
          }

          readiness_probe {
            http_get {
              path = "/health"
              port = 8000
            }
            initial_delay_seconds = 5
            period_seconds        = 10
            failure_threshold     = 5
          }

          liveness_probe {
            http_get {
              path = "/health"
              port = 8000
            }
            initial_delay_seconds = 15
            period_seconds        = 15
          }

          resources {
            requests = {
              cpu    = "100m"
              memory = "256Mi"
            }
            limits = {
              memory = "768Mi"
            }
          }
        }
      }
    }
  }
}

# ClusterIP: this service is only ever called by the backend, server to server.
# Keeping it unexposed is the same reasoning as in the kubectl manifests.
resource "kubernetes_service" "ml_service" {
  metadata {
    name      = "ml-service"
    namespace = kubernetes_namespace.atb.metadata[0].name
    labels    = local.ml_labels
  }

  spec {
    type     = "ClusterIP"
    selector = local.ml_labels

    port {
      name        = "http"
      port        = 8000
      target_port = 8000
    }
  }
}
