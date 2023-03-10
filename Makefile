.PHONY: all setup update major clean dev serve open test deploy

ifeq ($(shell uname -s),Linux)
  OPEN := xdg-open
else
  OPEN := open
endif

all: setup dev

setup:
	clojure -P
	npm ci

update:
	clojure -M:outdated --every --write
	npm update --include=dev

major:
	ncu -u

clean:
	rm -rf target/public

min: clean
	clojure -M:prod

dev:
	clojure -M:dev

serve: clean
	clojure -M:serve

open:
	gh run watch && ${OPEN} https://timothypratley.github.io/montyhall || ${OPEN} https://github.com/timothypratley/montyhall

deploy: setup min
	cd resources/public && \
	mkdir -p cljs-out/webapp && \
	cp ../../target/public/cljs-out/webapp/main_bundle.js cljs-out/webapp/main_bundle.js && \
	rm -fr .git && \
	git init && \
	git add . && \
	git commit -m "Deploy to GitHub Pages" && \
	git push --force --quiet "git@github.com:timothypratley/montyhall.git" main:gh-pages && \
	rm -fr .git && \
	rm -fr cljs-out && \
	echo https://timothypratley.github.io/montyhall

