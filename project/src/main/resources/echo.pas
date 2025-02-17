program echo;

var 
    myVar : integer;

begin
    writeln ('Enter a number to echo it. Ctrl+C to quit!');
    myVar := 1;
    While true do 
    begin
        readln (myVar);
        writeln (myVar);
    end;
end.