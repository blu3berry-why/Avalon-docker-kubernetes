kubectl apply -f configmaps\avalon-configmap.yaml
kubectl apply -f configmaps\mongo-config-map.yaml

kubectl apply -f secrets\avalon-secret.yaml
kubectl apply -f secrets\mongo-secret.yaml

kubectl apply -f deployment\gamelogic-deployment.yaml
kubectl apply -f deployment\mongo-deployment.yaml
kubectl apply -f deployment\email-handler-deployment.yaml

kubectl apply -f ingess\avalon-ingress.yaml

PAUSE
