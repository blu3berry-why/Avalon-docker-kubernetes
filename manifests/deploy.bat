::prometheus
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

cd ..
kubectl create -f prometheus/manifests/setup/
kubectl create -f prometheus/manifests/
cd manifests

helm repo add traefik https://helm.traefik.io/traefik
helm repo update

helm install traefik traefik/traefik

:: configmaps
kubectl apply -f configmaps\avalon-configmap.yaml
kubectl apply -f configmaps\mongo-config-map.yaml

:: secrets
kubectl apply -f secrets\avalon-secret.yaml
kubectl apply -f secrets\mongo-secret.yaml

:: Deployments
kubectl apply -f deployment\gamelogic-deployment.yaml
REM kubectl apply -f deployment\mongo-deployment.yaml
kubectl apply -f deployment\email-handler-deployment.yaml

:: StorageClasses
kubectl apply -f storageclasses\storageclass.yaml

:: StatefulSets
kubectl apply -f statefulsets\mongo.yaml

:: Ingress
kubectl apply -f ingress\avalon-ingress.yaml

:: Exporter
helm install mongodb-exporter prometheus-community/prometheus-mongodb-exporter -f exporter/values.yaml

PAUSE
