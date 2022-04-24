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

PAUSE
