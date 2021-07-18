# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.4.2] - 2021-07-08
### Added
- Parse arguments to keyword.
- Added remaining examples to demo project.

## [1.4.1] - 2021-05-25
### Added
- Demo project to main repository.

## [1.4.0] - 2021-03-18
### Added
- Generalized meta extraction from components.  This can be used to e.g. collect css for minification.

## [1.3.0] - 2021-03-01
### Added
- Enhanced hiccup rending.  Important htmx attributes will now be rendered as expected by default.

## [1.2.0] - 2021-02-21
### Added
- Middleware to pre-process requests before they are handed to destructing arguments.

## [1.1.1] - 2021-01-27
### Changed
- Removed annoying dyanamic vars.  Can use lazy evaluation without restriction.

## [1.1.0] - 2021-01-11
### Added
- Basic support for node with cljs.

## [1.0.0] - 2020-12-27
### Added
- Defined basic CTMX API.
- Added component stack tracking, with conversion to and from nested JSON.