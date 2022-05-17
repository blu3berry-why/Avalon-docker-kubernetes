kubectl apply -f namespace.yaml
kubectl apply -f crds/crd.yaml
kubectl apply -f rbac
kubectl apply -f operator.yaml
kubectl apply -f internal/secret.yaml
kubectl apply -f internal/mongodb.yaml

PAUSE