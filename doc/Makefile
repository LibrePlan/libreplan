
BUILDDIR ?= _build

PACKAGE  := documento-usuario-navalplan
VERSION  := 0.1

all_rst   := $(wildcard src/*/*.rst)
main_rst  := src/documento-usuario-navalplan.rst

all_pdf   := $(patsubst src/%.rst,$(BUILDDIR)/%.pdf,$(all_rst))
main_pdf  := $(patsubst src/%.rst,$(BUILDDIR)/%.pdf,$(main_rst))
ebook_pdf := $(patsubst src/%.rst,$(BUILDDIR)/%.ebook.pdf,$(main_rst))

all_html  := $(patsubst src/%.rst,$(BUILDDIR)/%.html,$(all_rst))
main_html := $(patsubst src/%.rst,$(BUILDDIR)/%.html,$(main_rst))

RST_TEX_FLAGS = --output-encoding=latin1 --font-encoding=OT1 \
								--use-latex-docinfo \
								--table-style=booktabs --use-latex-toc \
								--use-latex-citations --use-latex-footnotes \
								--documentclass=igaliabk \
								$(RST_EXTRA_TEX_FLAGS)

RST_HTML_FLAGS = --output-encoding=utf8 \
								 --stylesheet-path=$(BUILDDIR)/css/lsr.css \
								 $(RST_EXTRA_HTML_FLAGS)


all: html pdf ebook build-att
pdf: $(all_pdf) $(main_pdf)
html: $(all_html) $(main_html)
ebook: $(ebook_pdf)

$(BUILDDIR)/documento-usuario-navalplan.pdf: $(all_rst)
$(BUILDDIR)/documento-usuario-navalplan.ebook.pdf: $(all_rst)


$(BUILDDIR)/documento-usuario-navalplan.html: src/parts $(all_rst)
	$P toplevel $@
	$Q (cd src && ../tools/toplevel-html) < $< \
		| ./tools/doctool rst2html $(RST_HTML_FLAGS) - $@

$(BUILDDIR)/documento-usuario-navalplan.tex: build-att
$(BUILDDIR)/documento-usuario-navalplan.tex: src/parts $(all_rst)
	$P toplevel $@
	$Q (cd src && ../tools/toplevel-tex) < $< \
	 | (cd $(BUILDDIR) && ../tools/doctool rst2latex $(RST_TEX_FLAGS) - ../$@)

$(BUILDDIR)/documento-usuario-navalplan.ebook.tex: build-att
$(BUILDDIR)/documento-usuario-navalplan.ebook.tex: src/parts $(all_rst)
	$P toplevel $@
	$Q (cd src && ../tools/toplevel-tex) < $< \
	 | (cd $(BUILDDIR) && ../tools/doctool rst2ebook $(RST_TEX_FLAGS) - ../$@)

all_img := $(wildcard src/*/images/*)
build_images :=

define imagedir-t
src_$1_images := $$(wildcard src/$1/images/*)
rst_$1_files  := $$(wildcard src/$1/*.rst)
pdf_$1_files  := $$(patsubst src/$1/%.rst,$(BUILDDIR)/$1/%.pdf,$$(rst_$1_files))
html_$1_files := $$(patsubst src/$1/%.rst,$(BUILDDIR)/$1/%.html,$$(rst_$1_files))
$1_images     := $$(patsubst src/%,$$(BUILDDIR)/%,$$(src_$1_images))

$$($1_images): $$(BUILDDIR)/$1/images/%: src/$1/images/%
	$$P copy-img $$@
	$$Q mkdir -p $$(@D)
	$$Q cp $$< $$@

$$(main_pdf): $$($1_images)
$$(main_html): $$($1_images)

$$(pdf_$1_files): $$($1_images)
$$(html_$1_files): $$($1_images)

endef


$(foreach name,$(patsubst src/%/images,%,$(wildcard src/*/images)),\
	$(eval $(call imagedir-t,$(name))))


define attachdir-t
src_$1_att := $$(wildcard src/$1/attachments/*)
dst_$1_att := $$(patsubst src/$1/%,$(BUILDDIR)/%,$$(src_$1_att))

$$(dst_$1_att): $$(BUILDDIR)/attachments/% : src/$1/attachments/%
	$$P copy-att $$@
	$$Q mkdir -p $$(@D)
	$$Q mkdir -p $$(BUILDDIR)/$1
	$$Q $$(RM) $$(BUILDDIR)/attachments/attachments
	$$Q if ! [ -L $$(BUILDDIR)/$1/attachments ] ; then \
	       rm -rf $$(BUILDDIR)/$1/attachments ; \
	       ln -s ../attachments $$(BUILDDIR)/$1/attachments ; \
	    fi
	$$Q cp $$< $$@

build-att: $$(dst_$1_att)
endef

build-att:

.PHONY: build-att

$(foreach name,$(patsubst src/%/attachments,%,$(wildcard src/*/attachments)),\
	$(eval $(call attachdir-t,$(name))))


clean:
	$P clean ''
	$Q $(RM) $(all_pdf) $(all_html) $(main_pdf) $(main_html) $(ebook_pdf)


$(BUILDDIR)/css/lsr.css: src/lsr.css
	$P copy-css $@
	$Q mkdir -p $(@D)
	$Q cp $< $@

$(BUILDDIR)/%.html: src/%.rst $(BUILDDIR)/css/lsr.css
	$P rst2html $@
	$Q mkdir -p $(@D)
	$Q ./tools/doctool rst2html $(RST_HTML_FLAGS) $< $@

$(BUILDDIR)/%.tex: src/%.rst
	$P rst2latex $@
	$Q mkdir -p $(@D)
	$Q ./tools/doctool rst2latex $(RST_TEX_FLAGS) $< $@

%.pdf: %.tex
	$P pdflatex $@
	$Q cd $(@D) && TEXINPUTS=.:$(CURDIR)/src: pdflatex $(PDFLATEX_FLAGS) $(CURDIR)/$<
	$Q cd $(@D) && TEXINPUTS=.:$(CURDIR)/src: pdflatex $(PDFLATEX_FLAGS) $(CURDIR)/$<


dist dist-pdf dist-html: \
	T = _dist/$(PACKAGE)-$(VERSION)/usr/share/$(PACKAGE)

dist:
	$P $@ $(PACKAGE)-$(VERSION)
	$Q mkdir -p $T/html
	$Q cp -r $(BUILDDIR)/* $T/html
	$Q rm -rf $T/html/attachments
	$Q find $T \( -name '*.log' \
		         -o -name '*.aux' \
		         -o -name '*.lot' \
		         -o -name '*.lof' \
		         -o -name '*.out' \
		         -o -name '*.toc' \
		         -o -name '*.pdf' \
		         -o -name '*.tex' \
		         \) -delete
	$Q find $T -type l -name attachments -delete
	$Q find $T/html -mindepth 1 -maxdepth 1 \
		      -type d -exec ln -fs ../../attachments {} \;
	$Q mkdir -p $T/pdf
	$Q cp $(main_pdf) $(all_pdf) $T/pdf
	$Q ln -sf ../attachments $T/pdf/attachments
	$Q mkdir -p $T/attachments
	$Q cp src/*/attachments/* $T/attachments
	$Q tar cfz $(PACKAGE)-$(VERSION).tar.gz -C _dist $(PACKAGE)-$(VERSION)

dist: all

installdirs:
	$P $@ ''
	$Q mkdir -p $(DESTDIR)/

install: dist installdirs
	$P $@ $(PACKAGE)-$(VERSION)
	$Q cp -aR _dist/$(PACKAGE)-$(VERSION)/. $(DESTDIR)/

distclean: clean
	$P $@ ''
	$Q $(RM) $(PACKAGE)-$(VERSION).tar.gz
	$Q $(RM) -r $(BUILDDIR)
	$Q $(RM) -r _dist


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


.PHONY: subdirs clean distclean install installdirs builddir pdf html
.PHONY: dist dist-pdf dist-html ebook

.SECONDARY:

