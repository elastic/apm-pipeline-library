FROM dependabot/dependabot-core:0.117.7

WORKDIR /src/dependabot-script
RUN git clone https://github.com/dependabot/dependabot-script . \
    && git checkout db966a00d0c65625008ff75d76526e9a9ad2cb63

RUN bundle install -j 3 --path vendor

CMD bundle exec ruby ./generic-update-script.rb
