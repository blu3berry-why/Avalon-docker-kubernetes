helm repo add jetstack https://charts.jetstack.io

helm repo update


kubectl create -f ./../../../prometheus/manifests/crd-mongo

kubectl apply -f namespace.yaml

helm install cert-105 jetstack/cert-manager --namespace cert-manager --version v1.6.1  --values helm-values.yaml

kubectl get pods -n cert-manager

PAUSE