program repetetive_statements;

var
    num, sqrNum, i: Integer;

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
end.