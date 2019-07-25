FROM python:3-alpine3.6

WORKDIR /yaml

RUN pip install yamllint && \
    rm -rf ~/.cache/pip

CMD ["yamllint", "--version"]
