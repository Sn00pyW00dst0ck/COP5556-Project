program repetetive_statements;

type
    TCardValues = (ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE);
var
    num, sqrNum, i: Integer;
    character: Char;
    card: TCardValues;

begin
    num := 1;
    sqrNum := num * num;

    writeln('While Statement');
    While sqrNum <= 100 do 
    begin
        write (sqrNum, ' ');
        num := num  + 1;
        sqrNum := num * num;
    end;
    writeln();

    writeln('Repeat Statement:');
    num := 1;
    sqrNum := num * num;

    Repeat  
        write (sqrNum, ' ');
        num := num  + 1;
        sqrNum := num * num;
    until sqrNum > 100;
    writeln();

    writeln('For Statement (to variant):');
    For i := 1 to 10 do 
    begin
        write(i, ' ');
    end;
    writeln();

    writeln('For Statement (down to variant):');
    For i := 10 downto 1 do 
    begin
        write(i, ' ');
    end;
    writeln();

    writeln('Loop over chars');
    For character := CHR(65) to CHR(90) do
        write(character);
    writeln();
    For character := CHR(122) downto CHR(97) do
        write(character);
    writeln();

    writeln('Loop over ENUMS');
    For card := ONE to ACE do
        write(card, ' ');
    writeln();
    For card := KING downto JACK do
        write(card, ' ');
    writeln();
end.