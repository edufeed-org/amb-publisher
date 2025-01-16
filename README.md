# AMB Publisher

Publishes AMB data from a json-file to some relay.

`clj -X publisher.core/run :path "resources/oersi_data.jsonl" :relay "ws://localhost:10547"`

## TODO

- [X] make relay configurable
- [ ] enable multiple relays
