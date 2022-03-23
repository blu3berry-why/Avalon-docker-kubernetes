kubectl delete deploy avalon-emailhandler
kubectl delete deploy gamelogic
kubectl delete deploy mongodb-deployment

kubectl delete secret avalon-secret
kubectl delete secret mongodb-secret

kubectl delete configmap avalon-configmap
kubectl delete ingress avalon-ingress

kubectl delete service avalon-emailhandler
kubectl delete service gamelogic
kubectl delete service mongodb-service

PAUSE