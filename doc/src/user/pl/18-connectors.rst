Łączniki
########

.. contents::

Łączniki to aplikacje klienckie *LibrePlan*, które mogą być używane do komunikacji z serwerami (sieciowymi) w celu pobierania danych, ich przetwarzania i przechowywania. Obecnie dostępne są trzy łączniki: łącznik JIRA, łącznik Tim Enterprise i łącznik e-mail.

Konfiguracja
============

Łączniki muszą być odpowiednio skonfigurowane przed ich użyciem. Można je skonfigurować na ekranie „Ustawienia główne" w zakładce „Łączniki".

Ekran łącznika zawiera:

*   **Lista rozwijana:** Lista dostępnych łączników.
*   **Ekran edycji właściwości:** Formularz edycji właściwości dla wybranego łącznika.
*   **Przycisk testowania połączenia:** Przycisk do testowania połączenia z łącznikiem.

Wybierz łącznik, który chcesz skonfigurować, z listy rozwijanej łączników. Zostanie wyświetlony formularz edytora właściwości dla wybranego łącznika. W formularzu edytora właściwości możesz zmieniać wartości właściwości według potrzeb i testować konfiguracje przy użyciu przycisku „Testuj połączenie".

.. NOTE::

   Właściwości są skonfigurowane z wartościami domyślnymi. Najważniejszą właściwością jest „Aktywowany". Domyślnie jest ustawiony na „N". Oznacza to, że łącznik nie będzie używany, chyba że zmienisz wartość na „Y" i zapiszesz zmiany.

Łącznik JIRA
============

JIRA to system śledzenia problemów i projektów.

Łącznik JIRA to aplikacja, która może być używana do żądania danych serwera JIRA dla problemów JIRA i przetwarzania odpowiedzi. Żądanie opiera się na etykietach JIRA. W JIRA etykiety mogą być używane do kategoryzowania problemów. Żądanie jest skonstruowane w następujący sposób: pobierz wszystkie problemy skategoryzowane przez tę nazwę etykiety.

Łącznik odbiera odpowiedź, która w tym przypadku stanowią problemy, i konwertuje je na „Elementy projektów" i „Karty czasu pracy" LibrePlan.

*Łącznik JIRA* musi być odpowiednio skonfigurowany przed użyciem.

Konfiguracja
------------

Na ekranie „Ustawienia główne" wybierz zakładkę „Łączniki". Na ekranie łączników wybierz łącznik JIRA z listy rozwijanej. Zostanie wyświetlony ekran edytora właściwości.

Na tym ekranie można skonfigurować następujące wartości właściwości:

*   **Aktywowany:** Y/N, wskazując, czy chcesz używać łącznika JIRA. Domyślnie „N".
*   **Adres URL serwera:** Bezwzględna ścieżka do serwera JIRA.
*   **Nazwa użytkownika i hasło:** Dane uwierzytelniające użytkownika do autoryzacji.
*   **Etykiety JIRA: lista etykiet oddzielonych przecinkami lub adres URL:** Możesz wprowadzić adres URL etykiety lub listę etykiet oddzielonych przecinkami.
*   **Typ godzin:** Typ godzin pracy. Domyślnie „Default".

.. NOTE::

   **Etykiety JIRA:** Obecnie serwer JIRA nie obsługuje dostarczania listy wszystkich dostępnych etykiet. Jako obejście opracowaliśmy prosty skrypt PHP, który wykonuje proste zapytanie SQL w bazie danych JIRA, aby pobrać wszystkie unikalne etykiety. Możesz użyć tego skryptu PHP jako „Adresu URL etykiet JIRA" lub wprowadzić żądane etykiety jako tekst oddzielony przecinkami w polu „Etykiety JIRA".

Na koniec kliknij przycisk „Testuj połączenie", aby sprawdzić, czy możesz połączyć się z serwerem JIRA i czy konfiguracje są poprawne.

Synchronizacja
--------------

W oknie projektu, w sekcji „Dane ogólne", możesz rozpocząć synchronizację elementów projektów z problemami JIRA.

Kliknij przycisk „Synchronizuj z JIRA", aby rozpocząć synchronizację.

*   Jeśli jest to pierwsze uruchomienie, zostanie wyświetlone wyskakujące okienko (z automatycznie uzupełnioną listą etykiet). W tym oknie możesz wybrać etykietę do synchronizacji i kliknąć przycisk „Rozpocznij synchronizację", aby rozpocząć proces synchronizacji, lub kliknąć przycisk „Anuluj", aby go anulować.

*   Jeśli etykieta jest już zsynchronizowana, na ekranie JIRA zostanie wyświetlona ostatnia data synchronizacji i etykieta. W tym przypadku nie zostanie wyświetlone wyskakujące okienko do wyboru etykiety. Zamiast tego proces synchronizacji rozpocznie się bezpośrednio dla tej wyświetlonej (już zsynchronizowanej) etykiety.

.. NOTE::

   Relacja między „Projektem" a „etykietą" jest jeden do jednego. Tylko jedna etykieta może być zsynchronizowana z jednym „Projektem".

.. NOTE::

   Po pomyślnej (re)synchronizacji informacje zostaną zapisane w bazie danych, a ekran JIRA zostanie zaktualizowany o ostatnią datę synchronizacji i etykietę.

(Re)synchronizacja jest wykonywana w dwóch fazach:

*   **Faza 1:** Synchronizacja elementów projektów, w tym przypisania postępu i pomiarów.
*   **Faza 2:** Synchronizacja kart czasu pracy.

.. NOTE::

   Jeśli Faza 1 zakończy się niepowodzeniem, Faza 2 nie zostanie wykonana i żadne informacje nie zostaną zapisane w bazie danych.

.. NOTE::

   Informacje o powodzeniu lub niepowodzeniu zostaną wyświetlone w wyskakującym okienku.

Po pomyślnym zakończeniu synchronizacji wynik zostanie wyświetlony w zakładce „Struktura podziału pracy (zadania SPP)" na ekranie „Szczegóły projektu". W tym interfejsie są dwie zmiany w stosunku do standardowego SPP:

*   Kolumna „Łączne godziny zadania" jest niemodyfikowalna (tylko do odczytu), ponieważ synchronizacja jest jednostronna. Godziny zadania można aktualizować tylko na serwerze JIRA.
*   Kolumna „Kod" wyświetla klucze problemów JIRA i są one również hiperlinkami do problemów JIRA. Kliknij żądany klucz, jeśli chcesz przejść do dokumentu dla tego klucza (problem JIRA).

Harmonogramowanie
-----------------

Resynchronizacja problemów JIRA może być również wykonywana przez harmonogram. Przejdź do ekranu „Harmonogramowanie zadań". Na tym ekranie możesz skonfigurować zadanie JIRA do wykonania synchronizacji. Zadanie wyszukuje ostatnio zsynchronizowane etykiety w bazie danych i odpowiednio je resynchronizuje. Zapoznaj się również z podręcznikiem harmonogramu.

Łącznik Tim Enterprise
======================

Tim Enterprise to holenderski produkt firmy Aenova. Jest to aplikacja internetowa do administrowania czasem spędzonym na projektach i zadaniach.

Łącznik Tim to aplikacja, która może być używana do komunikacji z serwerem Tim Enterprise w celu:

*   Eksportowania wszystkich godzin spędzonych przez pracownika (użytkownika) na projekcie, które mogą być zarejestrowane w Tim Enterprise.
*   Importowania wszystkich harmonogramów pracownika (użytkownika) w celu efektywnego planowania zasobu.

*Łącznik Tim* musi być odpowiednio skonfigurowany przed użyciem.

Konfiguracja
------------

Na ekranie „Ustawienia główne" wybierz zakładkę „Łączniki". Na ekranie łączników wybierz łącznik Tim z listy rozwijanej. Zostanie wyświetlony ekran edytora właściwości.

Na tym ekranie można skonfigurować następujące wartości właściwości:

*   **Aktywowany:** Y/N, wskazując, czy chcesz używać łącznika Tim. Domyślnie „N".
*   **Adres URL serwera:** Bezwzględna ścieżka do serwera Tim Enterprise.
*   **Nazwa użytkownika i hasło:** Dane uwierzytelniające użytkownika do autoryzacji.
*   **Liczba dni karty czasu pracy do Tim:** Liczba dni wstecz, dla których chcesz eksportować karty czasu pracy.
*   **Liczba dni harmonogramu z Tim:** Liczba dni do przodu, dla których chcesz importować harmonogramy.
*   **Współczynnik produktywności:** Efektywny czas pracy w procentach. Domyślnie „100%".
*   **Identyfikatory działów do importu harmonogramu:** Identyfikatory działów oddzielone przecinkami.

Na koniec kliknij przycisk „Testuj połączenie", aby sprawdzić, czy możesz połączyć się z serwerem Tim Enterprise i czy konfiguracje są poprawne.

Eksport
-------

W oknie projektu, w sekcji „Dane ogólne", możesz rozpocząć eksportowanie kart czasu pracy do serwera Tim Enterprise.

Wprowadź „Kod produktu Tim" i kliknij przycisk „Eksportuj do Tim", aby rozpocząć eksport.

Łącznik Tim dodaje następujące pola wraz z kodem produktu:

*   Pełne imię i nazwisko pracownika/użytkownika.
*   Data, w której pracownik pracował nad zadaniem.
*   Wysiłek lub godziny pracy nad zadaniem.
*   Opcja wskazująca, czy Tim Enterprise powinien zaktualizować rejestrację, czy wstawić nową.

Odpowiedź Tim Enterprise zawiera tylko listę identyfikatorów rekordów (liczby całkowite). Trudno jest określić, co poszło nie tak, ponieważ lista odpowiedzi zawiera tylko liczby niezwiązane z polami żądania. Zakłada się, że żądanie eksportu (rejestracja w Tim) powiodło się, jeśli wszystkie wpisy na liście nie zawierają wartości „0". W przeciwnym razie żądanie eksportu nie powiodło się dla tych wpisów, które zawierają wartości „0". Dlatego nie można zobaczyć, które żądanie nie powiodło się, ponieważ wpisy na liście zawierają tylko wartość „0". Jedynym sposobem ustalenia tego jest sprawdzenie pliku dziennika na serwerze Tim Enterprise.

.. NOTE::

   Po pomyślnym wyeksportowaniu informacje zostaną zapisane w bazie danych, a ekran Tim zostanie zaktualizowany o ostatnią datę eksportu i kod produktu.

.. NOTE::

   Informacje o powodzeniu lub niepowodzeniu zostaną wyświetlone w wyskakującym okienku.

Harmonogramowanie eksportu
--------------------------

Proces eksportu może być również wykonywany przez harmonogram. Przejdź do ekranu „Harmonogramowanie zadań". Na tym ekranie możesz skonfigurować zadanie eksportu Tim. Zadanie wyszukuje ostatnio eksportowane karty czasu pracy w bazie danych i odpowiednio je re-eksportuje. Zapoznaj się również z podręcznikiem harmonogramu.

Import
------

Importowanie harmonogramów działa tylko przy pomocy harmonogramu. Nie ma interfejsu użytkownika do tego przeznaczonego, ponieważ nie jest potrzebny żaden wkład od użytkownika. Przejdź do ekranu „Harmonogramowanie zadań" i skonfiguruj zadanie importu Tim. Zadanie przechodzi przez wszystkie działy skonfigurowane we właściwościach łącznika i importuje wszystkie harmonogramy dla każdego działu. Zapoznaj się również z podręcznikiem harmonogramu.

Do importu łącznik Tim dodaje następujące pola w żądaniu:

*   **Okres:** Okres (data od — data do), dla którego chcesz importować harmonogram. Może być podany jako kryterium filtru.
*   **Dział:** Dział, dla którego chcesz importować harmonogram. Działy są konfigurowalne.
*   Pola, które Cię interesują (jak informacje o osobie, kategoria harmonogramu itp.), które serwer Tim powinien uwzględnić w odpowiedzi.

Odpowiedź importu zawiera następujące pola, które są wystarczające do zarządzania dniami wyjątkowymi w *LibrePlan*:

*   **Informacje o osobie:** Imię i nazwa sieciowa.
*   **Dział:** Dział, w którym pracuje pracownik.
*   **Kategoria harmonogramu:** Informacje o obecności/nieobecności (Aanwzig/afwezig) pracownika i powodzie (*typ wyjątku LibrePlan*) w przypadku nieobecności pracownika.
*   **Data:** Data, w której pracownik jest obecny/nieobecny.
*   **Czas:** Czas rozpoczęcia obecności/nieobecności, na przykład 08:00.
*   **Czas trwania:** Liczba godzin, przez które pracownik jest obecny/nieobecny.

Konwertując odpowiedź importu na „Dzień wyjątkowy" *LibrePlan*, brane są pod uwagę następujące tłumaczenia:

*   Jeśli kategoria harmonogramu zawiera nazwę „Vakantie", zostanie przetłumaczona na „URLOP ZASOBU".
*   Kategoria harmonogramu „Feestdag" zostanie przetłumaczona na „ŚWIĘTO USTAWOWE".
*   Wszystkie pozostałe, jak „Jus uren", „PLB uren" itp., powinny być dodane do „Dni wyjątkowych kalendarza" ręcznie.

Ponadto w odpowiedzi importu harmonogram jest podzielony na dwie lub trzy części na dzień: na przykład harmonogram-rano, harmonogram-popołudnie i harmonogram-wieczór. Jednak *LibrePlan* pozwala tylko na jeden „Typ wyjątku" na dzień. Łącznik Tim jest wtedy odpowiedzialny za scalenie tych części jako jeden typ wyjątku. To znaczy, zakłada się, że kategoria harmonogramu o największym czasie trwania jest prawidłowym typem wyjątku, ale łączny czas trwania to suma wszystkich czasów trwania tych części kategorii.

W przeciwieństwie do *LibrePlan*, w Tim Enterprise łączny czas trwania w przypadku urlopu pracownika oznacza, że pracownik nie jest dostępny przez ten łączny czas trwania. Jednak w *LibrePlan*, jeśli pracownik jest na urlopie, łączny czas trwania powinien wynosić zero. Łącznik Tim obsługuje również to tłumaczenie.

Łącznik e-mail
==============

E-mail to metoda wymiany cyfrowych wiadomości od autora do jednego lub więcej odbiorców.

Łącznik e-mail może być używany do ustawiania właściwości połączenia serwera Simple Mail Transfer Protocol (SMTP).

*Łącznik e-mail* musi być odpowiednio skonfigurowany przed użyciem.

Konfiguracja
------------

Na ekranie „Ustawienia główne" wybierz zakładkę „Łączniki". Na ekranie łączników wybierz łącznik e-mail z listy rozwijanej. Zostanie wyświetlony ekran edytora właściwości.

Na tym ekranie można skonfigurować następujące wartości właściwości:

*   **Aktywowany:** Y/N, wskazując, czy chcesz używać łącznika e-mail. Domyślnie „N".
*   **Protokół:** Typ protokołu SMTP.
*   **Host:** Bezwzględna ścieżka do serwera SMTP.
*   **Port:** Port serwera SMTP.
*   **Adres nadawcy:** Adres e-mail nadawcy wiadomości.
*   **Nazwa użytkownika:** Nazwa użytkownika serwera SMTP.
*   **Hasło:** Hasło serwera SMTP.

Na koniec kliknij przycisk „Testuj połączenie", aby sprawdzić, czy możesz połączyć się z serwerem SMTP i czy konfiguracje są poprawne.

Edytowanie szablonu e-mail
--------------------------

W oknie projektu, w sekcji „Konfiguracja", a następnie „Edytuj szablony e-mail", możesz modyfikować szablony e-mail dla wiadomości.

Możesz wybrać:

*   **Język szablonu:**
*   **Typ szablonu:**
*   **Temat e-maila:**
*   **Zawartość szablonu:**

Musisz podać język, ponieważ aplikacja internetowa będzie wysyłać e-maile do użytkowników w języku, który wybrali w swoich preferencjach. Musisz wybrać typ szablonu. Typ to rola użytkownika, co oznacza, że ten e-mail będzie wysyłany tylko do użytkowników posiadających wybraną rolę (typ). Musisz ustawić temat e-maila. Temat to krótkie podsumowanie tematu wiadomości. Musisz ustawić zawartość e-maila. To są wszelkie informacje, które chcesz wysłać do użytkownika. Istnieją również słowa kluczowe, których możesz używać w wiadomości; aplikacja internetowa będzie je parsować i ustawiać nową wartość zamiast słowa kluczowego.

Harmonogramowanie e-maili
--------------------------

Wysyłanie e-maili może być wykonywane tylko przez harmonogram. Przejdź do „Konfiguracji", a następnie do ekranu „Harmonogramowanie zadań". Na tym ekranie możesz skonfigurować zadanie wysyłania e-maili. Zadanie pobiera listę powiadomień e-mail, zbiera dane i wysyła je na adres e-mail użytkownika. Zapoznaj się również z podręcznikiem harmonogramu.

.. NOTE::

   Informacje o powodzeniu lub niepowodzeniu zostaną wyświetlone w wyskakującym okienku.
