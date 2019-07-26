FROM python:3.7.4-alpine3.10

WORKDIR /yaml

RUN pip install yamllint==1.16.0 && \
    rm -rf ~/.cache/pip

ENTRYPOINT ["yamllint"]
CMD ["--version"]
