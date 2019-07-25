FROM python:3-alpine3.6

WORKDIR /yaml

RUN pip install yamllint==1.16.0 && \
    rm -rf ~/.cache/pip

ENTRYPOINT ["yamllint"]
CMD ["--version"]
