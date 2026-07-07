resource "kind_cluster" "this" {
  name           = var.cluster_name
  wait_for_ready = true
  node_image     = "kindest/node:v1.30.0"
}
