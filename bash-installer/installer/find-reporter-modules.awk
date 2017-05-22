#!/usr/bin/awk -f

# Finds reporter classes in cassandra-diagnostics YAML configuration file.

BEGIN {
    reporters_block_indentation = 0
    in_reporters_block = 0
    reporters_block_items_indentation = 0
    should_detect_reporters_block_items_indentation = 0
    reporters = ""
    ignore_reporter = "LogReporter"
}

{
    gsub(/\t/, "  ") # Replace tabs with two spaces.
    current_line = $0

    if(is_commented_out(current_line)) {
        next
    }

    if(reporters_block_starts_on(current_line)) {
        in_reporters_block = 1
        should_detect_reporters_block_items_indentation = 1
        reporters_block_indentation = indentation_level_of(current_line)
    } else {
      if(should_detect_reporters_block_items_indentation) {
        reporters_block_items_indentation = indentation_level_of(current_line)
        should_detect_reporters_block_items_indentation = 0
      }

        if(in_reporters_block) {
            if(indentation_level_of(current_line) <= reporters_block_indentation) {
                in_reporters_block = 0
                reporters_block_items_indentation = 0
            } else if(indentation_level_of(current_line) > reporters_block_items_indentation) {
              next
            } else if(starts_with_dash(current_line)) {
              reporters = find_reporters_using(reporters, current_line)
            } else {
              next
            }
        }
    }
}

END {
    print reporters
}


# Functions
function indentation_level_of(line) {
    whitespace_count = 0

    for(i = 0; i < length(line); i++) {
        if(char_at(line, i + 1) == " ") {
            whitespace_count++
        } else {
            break
        }
    }

    return whitespace_count
}

function is_commented_out(line) {
    return match(current_line, "\\W#.")
}

function starts_with_dash(line) {
  return match(current_line, "\\W-.")
}

function char_at(string, i) {
    return substr(string,i,1);
}

function reporters_block_starts_on(line) {
    return index(line, "reporters:") > 0
}

function find_reporters_using(reporters_list, reporter_configuration_line) {
    tokens_count = split(reporter_configuration_line, tokens, ".")
    reporter = tokens[tokens_count]

    if(reporter == ignore_reporter) {
        return reporters_list
    }

    if(index(reporters_list, reporter) == 0) {
        return reporters_list reporter " "
    }

    return reporters_list
}
