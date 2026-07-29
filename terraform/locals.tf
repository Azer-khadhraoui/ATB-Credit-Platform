# Locals are computed once and reused: these labels appear in the Deployment metadata, its
# selector, the pod template and the Service selector. Written out four times they would drift;
# the Service would silently stop matching the pods the day one copy changed.
locals {
  ml_labels = {
    "app.kubernetes.io/name"       = "ml-service"
    "app.kubernetes.io/part-of"    = "atb-credit-platform"
    "app.kubernetes.io/managed-by" = "terraform"
  }
}
