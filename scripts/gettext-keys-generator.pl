#!/usr/bin/perl

# Copyright (C) 2009 Diego Pino García <dpino@igalia.com>
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

# Parses ZUL files for REG_EXPS and adds ENTRIES to a keys.pot file
# (respecting keys.pot format)
#
# If keys.pot exists, appends new elements to it
#
# If a msgid exists inside keys.pot file, updates its list of files
# pointing to that entry
#
# Options:
#       --keys, -k    point to an existing keys.pot file, creates a new one otherwise
#       --dir, -d     path to root file containing ZUL files, gets everything under

use File::Spec;
use Getopt::Long qw(:config gnu_getopt no_ignore_case);
use Date::Format;
use HTML::Entities;

my $DEBUG = 0;
my $TOKEN = 'i18n:_';
my @REG_EXPS = qw(i18n:_{1,2}\\('(.*?)'.*?\\) i18n:_{1,2}\\("(.*?)".*?\\) ganttzk_i18n:_{1,2}\\('(.*?)'.*?\\) ganttzk_i18n:_{1,2}\\("(.*?)".*?\\) <i18n\s.*?value='(.*?)' <i18n\s.*?value="(.*?)");
my $DEFAULT_KEYS_FILE = "./keys.pot";
my %ENTRIES;

GetOptions('dir|d=s' => \$OPTS{'DIR'},
           'help|h!' => \$OPTS{'HELP'},
           'java!' => \$OPTS{'JAVA'},
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
    &create_keys_file($OPTS{'KEYS'}, $header) || die("Could not create file: $OPTS{'KEYS'}");
} else {
    &create_keys_file($OPTS{'KEYS'}) || die("Could not create file: $OPTS{'KEYS'}");
}

# Validation option is selected
if ($OPTS{'JAVA'}) {
    # Parse java files searching for hibernate validations strings
    @filenames = split "\n", `find $OPTS{'DIR'} -name "*.java" | grep -v target`;
    map {&parse_hibernate_validations($_)} @filenames;
} else {
    # Parse zul files searching for i18n tokens
    @filenames = split "\n", `find $OPTS{'DIR'} -name "*.zul" | grep -v target`;
    map {&parse_ZUL($_)} @filenames;
}
&debug("Total entries: ".scalar keys %ENTRIES);

&generate_keys_pot_file($OPTS{'KEYS'});

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
    my $key = "";

    open FILE, $keys_filename;
    while (<FILE>) {
        chomp;
        if (/^#:\s+(.*)/) {
# &debug("filenames: ".$1);
            push @filenames, $1;
        }
        if (/^msgid "(.*?)"$/) {
# &debug("msgid: $1");
            $key = "\"".decode_entities($1)."\"";
        }
        if (/^"(.*?)"$/) {
            $key .= "\n"."\"".decode_entities($1)."\"";
        }
        if (/^msgstr/) {
            &debug("msgid: $key");
            $ENTRIES{$key} = [@filenames];
            @filenames = ();
        }
    }
    close FILE;
}

##
# Parses ZUL file and stores elements successfully parsed into ENTRIES array
#
# %ENTRIES is an associative array storing:
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
    $filename = File::Spec->rel2abs(&trim($filename));

    open FILE, $filename;
    @lines = <FILE>;
    close FILE;

    my $numline = 1;
    foreach $line (@lines) {
        $line = &trim($line);
        foreach $regexp (@REG_EXPS) {
            ($msgid) = $line =~ /$regexp/;

            if ($msgid ne "") {
                $msgid = '"'.decode_entities($msgid).'"';
                &addEntry($msgid, &to_relative_path($filename).":".$numline);
            }
        }
        $numline++;
    }
}

sub to_relative_path()
{
    my($filename) = @_;

    if ($filename =~ /(.*)\/(ganttzk|navalplanner-webapp|navalplanner-business)\/(.*)/) {
        return "./$2/$3";
    }
    return $filename;
}

sub addEntry()
{
    my($msgid, $filename) = @_;

# print "msgid: $msgid\n";
    if (!$ENTRIES{$msgid}) {
        # Create new array with element filename
        $ENTRIES{$msgid} = [($filename)];
    } else {
        if (!&in_array($filename, @{$ENTRIES{$msgid}})) {
            # Append filename to array of filenames in that entry
            $ENTRIES{$msgid} = [(@{$ENTRIES{$msgid}}, $filename)];
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

sub trim($)
{
    my $string = shift;
    $string =~ s/^\s+//;
    $string =~ s/\s+$//;
    return $string;
}

sub parse_hibernate_validations()
{
    my($filename) = @_;
    $filename = File::Spec->rel2abs(&trim($filename));

    open FILE, $filename;
    @lines = <FILE>;
    close FILE;

    my $open = 0;
    my $total = scalar @lines;
    for ($i = 0; $i < $total; $i++) {
        my $line = &trim($lines[$i]);

        # Skip comments
        if ($line =~ /^\s*\/\//) {
            next;
        }
        # First line
        if ($line =~ /\@\w+\(message=\"(.*?)\"/) {
            $msgid = "\"$1\"";
            # Line ends with a )
            if ($line =~ /\)\s*$/) {
                &addEntry($msgid, &to_relative_path($filename).":".($i+1));
                next;
            }
            $open = 1;
            next;
        }
        # Line starts may start with a plus and quoted string
        if ($open == 1 && $line =~ /^\s*\+?\s*\"(.*?)\"/) {
            $msgid .= "\n\"$1\"";
            # Line ends with a )
            if ($line =~ /\)\s*$/) {
                $open = 0;
                &addEntry($msgid, &to_relative_path($filename).":".($i+1));
                next;
            }
        }
        # Line ends with quoted string and a plus
        if ($open == 1 && $line =~ /\"(.*?)\"\s*\+\s*$/) {
            $msgid .= "\n\"$1\"";
            next;
        }
    }
}

sub generate_keys_pot_file()
{
    my($keys_filename) = @_;

    # Open keys.pot file to append
    open FILE, ">>$keys_filename" || die("Could not open file: $keys_filename");
    # Print ENTRIES to file
    foreach $msgid (keys %ENTRIES) {
        if ($msgid eq "") {
            next;
        }
        foreach $filename (@{$ENTRIES{$msgid}}) {
            print FILE "\#: $filename\n";
        }
        print FILE "msgid $msgid\n";
        print FILE "msgstr \"\"\n\n";
    }
    close FILE;
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
    print "Parses ZUL files searching for gettext ENTRIES and append to keys.pot file\n";
    print "\t--dir, -d\tMANDATORY\tBase directory\n";
    print "\t--keys, -k\tOPTIONAL\tPath to keys.pot file\n";
    print "\t--java\tOPTIONAL\tParses java files (default: parses zul files)\n";
    print "\t--help, -h\tOPTIONAL\tShow this help\n";

    exit();
}
