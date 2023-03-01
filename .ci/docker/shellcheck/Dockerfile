# Resulting Alpine image
FROM alpine:3.10.1
COPY --from=koalaman/shellcheck:v0.9.0@sha256:a527e2077f11f28c1c1ad1dc784b5bc966baeb3e34ef304a0ffa72699b01ad9c /bin/shellcheck /bin
RUN /bin/shellcheck -V
WORKDIR /mnt

ENTRYPOINT ["/bin/shellcheck"]
CMD ["-V"]
