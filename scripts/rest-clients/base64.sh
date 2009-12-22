#!/usr/bin/ruby

require 'base64'
puts Base64.encode64("#{ARGV[0]}");
