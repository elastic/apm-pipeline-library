FROM alpine:3.9

RUN apk update && apk add bash curl git
RUN curl -o /usr/local/bin/codecov.sh https://codecov.io/bash
RUN chmod +x /usr/local/bin/codecov.sh
WORKDIR /app
ENTRYPOINT [ "/usr/local/bin/codecov.sh" ]
CMD ["-h"]
