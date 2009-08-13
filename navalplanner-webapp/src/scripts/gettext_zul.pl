#!/usr/bin/perl

# Parses ZUL files for TOKEN and adds entries to a keys.pot file
# (respecting keys.pot format)
#
# If keys.pot exists, appends new elements to it
#
# If a msgid exists inside keys.pot file, updates its list of files
# pointing to that entry
#

# Copyright (C) 2008 Diego Pino García <dpino@igalia.com>
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

use File::Spec;
use Getopt::Long qw(:config gnu_getopt no_ignore_case);
use Date::Format;

$DEBUG = 0;
$TOKEN = 'i18n:_';
$DEFAULT_KEYS_FILE = "./keys.pot";

GetOptions('dir|d=s' => \$OPTS{'DIR'},
           'help|h!' => \$OPTS{'HELP'},
           'keys|k=s' => \$OPTS{'KEYS'});

if ($OPTS{'HELP'} || !$OPTS{'DIR'}) {
    &help();
}

if (!$OPTS{'KEYS'}) {
    $OPTS{'KEYS'} = $DEFAULT_KEYS_FILE;
}

# keys.pot file exists
if (-s $OPTS{'KEYS'} != 0) {
    &parse_KEYS($OPTS{'KEYS'});
    $header = &get_keys_header($OPTS{'KEYS'});
    &create_keys_file($OPTS{'KEYS'}, $header);
} else {
    &create_keys_file($OPTS{'KEYS'});
}

# Open keys.pot file to append
open FILE, ">>$OPTS{'KEYS'}";

# Find all .zul files from dir
@filenames = split "\n", `find $OPTS{'DIR'} -name "*.zul" | grep -v target`;
# Parse all files and store parsed elements into %entries
map {&parse_ZUL($_)} @filenames;

# Print entries to file
foreach $msgid (keys %entries) {
    foreach $filename (@{$entries{$msgid}}) {
        print FILE "\#: $filename\n";
    }
    print FILE "msgid \"$msgid\"\n";
    print FILE "msgstr \"\"\n\n";
}

close FILE;

##########################################################

sub create_keys_file()
{
    my ($filename, $header) = @_;

    if ($filename eq "") {
# &debug("filename: $filename");
        $filename = $DEFAULT_KEYS_FILE;
    }
    if ($header eq "") {
# &debug("header: $header");
        $header = &get_default_keys_header();
    }

    open FILE, ">$filename";
    print FILE $header;
    close FILE;
}

sub get_default_keys_header()
{
    $creation_date = time2str("%Y-%m-%d %H:%M %z", time);

    # Create file
    open FOUT, ">$OPTS{'KEYS'}";
print FOUT qq#\# SOME DESCRIPTIVE TITLE.
\# Copyright (C) YEAR THE PACKAGE'S COPYRIGHT HOLDER
\# This file is distributed under the same license as the PACKAGE package.
\# FIRST AUTHOR <EMAIL\@ADDRESS>, YEAR.
\#
\#, fuzzy
msgid ""
msgstr ""
"Project-Id-Version: PACKAGE VERSION\\n"
"Report-Msgid-Bugs-To: \\n"
"POT-Creation-Date: $creation_date\\n"
"PO-Revision-Date: YEAR-MO-DA HO:MI+ZONE\\n"
"Last-Translator: FULL NAME <EMAIL\@ADDRESS>\\n"
"Language-Team: LANGUAGE <LL\@li.org>\\n"
"MIME-Version: 1.0\\n"
"Content-Type: text/plain; charset=CHARSET\\n"
"Content-Transfer-Encoding: 8bit\\n"
\n#;
}

##
# Gets a keys.pot header from keys.pot file
#
# A keys.pot header happens at the beggining of the keys.pot file till the first occurrence
# of an entry (marked by the symbol #:
#
sub get_keys_header()
{
    my ($filename) = @_;

    open FILE, $filename;
    while ( ($line = <FILE>) && !($line =~ /^#:\s+/) ) {
        $header .= $line;
    }
    close FILE;

    return $header;
}

##
# Parse a key.pot file. A keys.pot file has the following structure
#
# (#: filename:line)+
# msgid "_msgid_"
#
sub parse_KEYS()
{
    my ($keys_filename) = @_;
    my @filenames = ();

    open FILE, $keys_filename;
    while (<FILE>) {
        if (/^#:\s+(.*)/) {
# &debug("filenames: ".$1);
            push @filenames, $1;
        }
        if (/^msgid "(\w+)"/) {
# &debug("msgid: $1");
            $entries{$1} = [@filenames];
            @filenames = ();
        }
    }
    close FILE;
}

##
# Parses ZUL file and stores elements successfully parsed into entries array
#
# %entries is an associative array storing:
#
# key, msgid (Message identifier, must be unique in a keys.pot file)
# value, array of filenames that references that entry
#
# Every element in array of filenames, is an entry of the following format:
# absolute_path_of_file:line_number
#
# @param filename ZUL file to parse
#
sub parse_ZUL()
{
    my($filename) = @_;

    @lines = split "\n",`grep -RHn "$TOKEN" $filename`;
    foreach $line (@lines) {
        &debug($line);
        # relative_path_to_file:line_number     match
        ($filename, $matching) = ($line =~ /(^[a-zA-Z\.\/_]+:[0-9]+):\s+(.*)/);
        ($msgid) = ($matching =~ /$TOKEN\('(\w+)'\)/);

        &debug("msgid: $msgid");
        &debug("filename: $filename");
        &debug("matching: $matching");

        # Get absolute path of file (passing line number aswell is OK)
        $filename = File::Spec->rel2abs(&trim($filename));
        if (!$entries{$msgid}) {
            # Create new array with element filename
            $entries{$msgid} = [($filename)];
        } else {
            if (!&in_array($filename, @{$entries{$msgid}})) {
                # Append filename to array of filenames in that entry
                $entries{$msgid} = [(@{$entries{$msgid}}, $filename)];
            }
        }
    }
}

sub in_array()
{
    my ($needle, @haystack) = @_;

    foreach (@haystack) {
        if ($_ eq $needle) {
            return 1;
        }
    }
    return 0;
}

sub debug()
{
    my($string) = @_;

    if ($DEBUG) {
        print $string."\n";
    }
}

sub help()
{
    print "Parses ZUL files searching for gettext entries and append to keys.pot file\n";
    print "\t--dir, -d\tMANDATORY\tBase directory\n";
    print "\t--keys, -k\tOPTIONAL\tPath to keys.pot file\n";
    print "\t--help, -h\tOPTIONAL\tShow this help\n";

    exit();
}

sub trim($)
{
    my $string = shift;
    $string =~ s/^\s+//;
    $string =~ s/\s+$//;
    return $string;
}
