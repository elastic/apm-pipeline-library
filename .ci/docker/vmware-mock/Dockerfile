FROM golang:1.17.7-alpine3.15

# download and compile vcsim and govc
# govc for govc find -l debug purposes
RUN go get -u github.com/vmware/govmomi/vcsim@v0.27.4 \
    && go get -u github.com/vmware/govmomi/govc@v0.27.4

# default exposed port is 443
EXPOSE 443

# run start command
ENTRYPOINT ["vcsim"]
CMD ["-l", "0.0.0.0:443"]
