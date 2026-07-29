# Terraform only knows what it created itself: its state file is its memory, not the cluster.
# Pointing it at the existing `atb` namespace would mean importing every object one by one,
# so it manages its own namespace instead. The kubectl-deployed stack stays untouched.
resource "kubernetes_namespace" "atb" {
  metadata {
    name = var.namespace

    labels = {
      "app.kubernetes.io/part-of"    = "atb-credit-platform"
      "app.kubernetes.io/managed-by" = "terraform"
    }
  }
}
