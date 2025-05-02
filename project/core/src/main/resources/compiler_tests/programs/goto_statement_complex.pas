program goto_statement_complex;
label 100, 200, 300, 400;

begin
    writeln('Start');

    begin
        100: writeln('At Label 100');
        goto 300;

        200: writeln('At Label 200');
        goto 400;

        300: writeln('At Label 300');
        goto 200;
    end;

    400: writeln('End of program');
end.