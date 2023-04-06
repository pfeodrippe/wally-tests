# Wally Tests

This repo contains some tests we are doing in the wild using
[Wally](https://github.com/pfeodrippe/wally).

## Book of Clerk

We are checking some parts of
https://github.clerk.garden/nextjournal/book-of-clerk (unofficially)
so we can harden Wally and also incentivize people to test projects
they love (and I love [Clerk](https://github.com/nextjournal/clerk)).

## Running tests locally

To run things locally, use

``` clojure
clj -M:test
```

## Running tests in the CI

When a new PR is opened or a merge happens to `main`, the tests are
run in headless mode in Github Actions, see the [the test
file](.github/workflows/test.yml).
