#!/usr/bin/ruby -w
# encoding: UTF-8

# Copyright (C) 2012 Diego Pino García <dpino@igalia.com>
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2
# of the License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 51 Franklin Street, Fifth Floor,
# Boston, MA  02110-1301, USA.

# Reads a files in keys.pot format and replaces msgid for msgstr in
# list of files marked with '#: '
#
# It doesn't work if msgid contains escaped characters
# Strings with several lines should be rewritten as one line only


require 'pp'

def readfile(filename)
    entries = Array.new
    entry = Hash.new
    files = Hash.new

    file = File.new(filename, "r")
    while (line = file.gets)
        line = line.strip
        if (/^msgstr "(.*?)"/.match(line))
            if (!$1.empty?)
                entry["msgstr"] = $1
                entry["files"] = files.keys
                entries.push(entry)

                entry = Hash.new
                files = Hash.new
            end
        elsif (/^msgid "(.*?)"/.match(line))
            if (!$1.empty?)
                entry["msgid"] = $1
            end
        elsif (/^#: (.*)/.match(line))
            if (!$1.empty?)
                parts = $1.split(":")
                key = parts[0]
                files[key] = ""
            end
        end
    end
    file.close

    return entries
end

def print_entries(entries)
    entries.each { |entry|
        puts "msgid: #{entry['msgid']}"
        puts "msgstr: #{entry['msgstr']}"
        puts "---\n"
    }
end

filename = "keys.pot";

entries = readfile(filename)
# print_entries(entries)

entries.each { |entry|
    entry["files"].each { |filename|
        filename = "../#{filename}"
        content = File.read(filename)
        content = content.gsub(entry["msgid"], entry["msgstr"])
        File.open(filename, "w") { |file| file.puts content}
    }
}
