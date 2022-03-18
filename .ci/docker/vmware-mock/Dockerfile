FROM golang:1.17.7-alpine3.15

COPY docker-entrypoint.sh .

# download and compile vcsim and govc
# govc for govc find -l debug purposes
RUN go get -u github.com/vmware/govmomi/vcsim \
    && go get -u github.com/vmware/govmomi/govc 

# default exposed port is 443
EXPOSE 443

# run start command
ENTRYPOINT ["./docker-entrypoint.sh"]