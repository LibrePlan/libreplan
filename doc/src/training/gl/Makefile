#
# Makefile
# Adrian Perez, 2009-05-15 11:20
#

RST_HTML_FLAGS = --link-stylesheet --stylesheet-path=html/lsr.css
RST_TEX_FLAGS  = --documentclass=igaliabk --font-encoding=OT1
OUTPUT_BASE    = output

rst_srcs   := $(filter-out index.rst,$(wildcard *.rst))
html_pages := $(patsubst %.rst,html/%.html,$(rst_srcs)) html/index.html

svg_images := $(wildcard images/*.svg)
png_images := $(patsubst %.svg,%.png,$(svg_images)) $(wildcard images/*.png)

utildir := ../../../tools/

# Main targets
#
all: html pdf
pdf: pdf/$(OUTPUT_BASE).pdf
ebook: pdf/$(OUTPUT_BASE).ebook.pdf
html: $(html_pages)
png: $(png_images)

.PHONY: html pdf ebook png

### Xavi

all_img := $(wildcard images/*)
build_images :=

src_images := $(wildcard images/*)
html_images := $(patsubst %,html/%,$(src_images))
pdf_images  := $(patsubst %,pdf/%,$(src_images))

$(html_images): html/images/%: images/%
	$P copy-img $@
	$Q mkdir -p $(@D)
	$Q cp $< $@

$(pdf_images):	pdf/images/%: images/%
	$P copy-img $@
	$Q mkdir -p $(@D)
	$Q cp $< $@

$(main_pdf): $(pdf_imges)



### End Xavi


# Cleanup targets
#
clean:
	$P clean pdf
	$Q $(RM) -r pdf/
	$P clean html
	$Q $(RM) -r html/
	$Q $(RM) index.rst

# Copy CSS file
#
html/lsr.css: lsr.css
	$P copy $@
	$Q mkdir -p $(@D)
	$Q cp $< $@

$(html_pages): html/lsr.css $(html_images)


# Top-level RST creation for the PDF
#
pdf/$(OUTPUT_BASE).rst: $(rst_srcs) docinfo
	$P toplevel $@
	$Q mkdir -p $(@D)
	$Q $(utildir)/doctool toplevel --info=docinfo $(rst_srcs) > $@

pdf/$(OUTPUT_BASE).pdf pdf/$(OUTPUT_BASE).ebook.pdf: $(png_images)

# Index page creation for the HTML output
#
index.rst: $(rst_srcs) docinfo
	$P htmlindex $@
	$Q mkdir -p $(@D)
	$Q $(utildir)/doctool htmlindex --info=docinfo $(rst_srcs) > $@


# Implicit rules
#
html/%.html: %.rst
	$P rst2html $@
	$Q mkdir -p $(@D)
	$Q $(utildir)/doctool rst2html $(RST_HTML_FLAGS) $< $@

%.tex: %.rst
	$P rst2latex $@
	$Q mkdir -p $(@D)
	$Q $(utildir)/doctool rst2latex $(RST_TEX_FLAGS) $< $@

%.ebook.tex: %.rst
	$P rst2ebook $@
	$Q mkdir -p $(@D)
	$Q $(utildir)/doctool rst2ebook $(RST_TEX_FLAGS) $< $@

%.pdf: %.tex
	$P pdflatex $@
	$Q cd $(@D) && TEXINPUTS=.:$(CURDIR): pdflatex $(PDFLATEX_FLAGS) $(CURDIR)/$<
	$Q cd $(@D) && TEXINPUTS=.:$(CURDIR): pdflatex $(PDFLATEX_FLAGS) $(CURDIR)/$<

%.trim.png: %.svg
	$P svg2png $@
	$Q inkscape --without-gui --export-png=$@ --export-area-canvas --export-dpi=150 $<

%.png: %.trim.png
	$P png-trim $@
	$Q convert -trim $< $@

# Avoid removing intermediate .tex files and similar things.
#
.SECONDARY:

# Control operation verboseness
#
ifeq ($(origin V),command line)
  verbose := $(V)
endif
ifndef verbose
  verbose := 0
endif

ifeq ($(verbose),0)
  P = @printf '[1;32m * [0;0m%-10s [1;33m%s[0;0m\n'
  Q = @
  PDFLATEX_FLAGS += -interaction batchmode
else
  P = @:
endif

# Check for the U= command line argument
#
ifeq ($(origin U),command line)
  utildir := $(U)
endif
ifndef utildir
  utildir := $(CURDIR)
endif


# vim:ft=make
#

