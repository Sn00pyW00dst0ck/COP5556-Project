program WhileLoopTest;
var
  i: integer;
begin
  i := 1;
  while i <= 5 do
  begin
    writeln('While loop iteration: ', i);
    i := i + 1;
  end;
end.
