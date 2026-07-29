# Variables are what make the same configuration serve several environments: change the value,
# not the code. Declared here with a type and a default; overridable per run or per tfvars file.
variable "namespace" {
  description = "Kubernetes namespace Terraform manages, kept separate from the kubectl-deployed one"
  type        = string
  default     = "atb-tf"
}
