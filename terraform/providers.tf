# Terraform itself does nothing: every resource type comes from a provider, a plugin that knows
# how to talk to one API. Here that API is the Kubernetes cluster — no cloud account involved,
# the same local cluster kubectl already targets.
terraform {
  required_version = ">= 1.5"

  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.35"
    }
  }
}

provider "kubernetes" {
  # Reuses the kubeconfig kubectl reads, so Terraform reaches the very same cluster.
  config_path    = "~/.kube/config"
  config_context = "docker-desktop"
}
