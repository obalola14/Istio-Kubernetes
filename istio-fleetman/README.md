***January 2023: should now support arm64 (Apple silicon) properly ***

For support, please visit the support service on the platform you're following the course on (Udemy or VPP). I generally check every day.

Now available at VirtualPairProgrammers.com and Udemy!

Udemy: https://www.udemy.com/course/istio-hands-on-for-kubernetes/?referralCode=36E4FA521FB5D6124156

VirtualPairProgrammers: https://virtualpairprogrammers.com/training-courses/Istio-training.html

Aim: make Istio understandable - it's not that hard. I don't mention TCP/IP stack levels once. Or the CNCF.

Conenect to cluster:
aws eks update-kubeconfig --region us-east-2 --name ex-eks-mng-al2

Steps to install istio:
helm install istiod istio/istiod -n istio-system  
                    OR 
helm upgrade istiod istio/istiod -n istio-system --create-namespace --install
helm install istio-base istio/base -n istio-system --set defaultRevision=default
helm install istio-ingress istio/gateway -n istio-ingress --create-namespace 

kubectl label namespace default "istio-injection=enabled"
For istio to work, add firewall rule 15017 from master to node groups

Kiali:
helm repo add kiali https://kiali.org/helm-charts
helm install \
    --set cr.create=true \
    --set cr.namespace=istio-system \
    --set cr.spec.auth.strategy="anonymous" \
    --namespace kiali-operator \
    --create-namespace \
    kiali-operator \
    kiali/kiali-operator
    