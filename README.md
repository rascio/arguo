# Arguo


![Caesar](cesare.jpg)
> **arguō** (present infinitive arguere, perfect active arguī, supine argūtum); third conjugation  
> I show, prove, assert, declare, make clear.  
(https://en.wiktionary.org/wiki/arguo)

## Guide

Look at the [tests](/test/arguo) folder for some example:

- [core_test.clj](/test/arguo/core_test.clj): Basic features
- [http_test.clj](/test/arguo/http_test.clj): HTTP features

## Features
- Flow mode
- Testing at the REPL
- HTTP templates (functions)
- Repeat tests for various inputs
- CSV/EDN inputs
- Relative assertion (usage of `*this*`)

## Backlog
- Tutorials:
	- setup a new project
	- test at the REPL
- Load configuration by environment
- Refactor CSV utility to help for:
	- parse values, currently every field is String
	- transform row->object[] as we can define more-than-one object from a row
	- configure escape/quote/newlines/BOM to read the CSV  	
	_It can be achieved passing options like:_
 	- `:row` Defining names of the rows (the current array of tokens)
 	- `:parsers` As an array of function to parse the same-index value
 	- `:transformer` A binding form to be used inside a `let`
 	- `:configuration` escape/quote/newlines/BOM stuffs
- HTML report

## In dev
- Templates for wiremock assertion
- Specs for validation

## Spikes
- Automatic tests with specs
