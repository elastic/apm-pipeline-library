# Build-only image
FROM ubuntu:18.04 AS build
USER root
WORKDIR /opt/shellCheck

# Install OS deps
RUN apt-get update -qq && apt-get install -qq -y ghc cabal-install git
RUN git clone -n https://github.com/koalaman/shellcheck.git .\
  && git checkout -B v0.6.0 cb57b4a74f490991e65ee8d0be1a6151a9819f91

# Install Haskell deps
RUN cabal update && cabal install --dependencies-only --ghc-options="-optlo-Os -split-sections"

# Copy source and build it
RUN cabal build Paths_ShellCheck && \
  ghc -optl-static -optl-pthread -isrc -idist/build/autogen --make shellcheck -split-sections -optc-Wl,--gc-sections -optlo-Os && \
  strip --strip-all shellcheck

RUN mkdir -p /out/bin && \
  cp shellcheck  /out/bin/

# Resulting Alpine image
FROM alpine:3.10.1
COPY --from=build /out /
RUN /bin/shellcheck -V
WORKDIR /mnt

ENTRYPOINT ["/bin/shellcheck"]
CMD ["-V"]
