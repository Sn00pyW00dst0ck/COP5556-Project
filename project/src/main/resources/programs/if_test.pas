program IfTest;

var
  x: integer;
  result: string;

begin
  x := 10;

  if x > 5 then
    result := 'Greater than 5'
  else
    result := 'Less or equal to 5';

  writeln(result);
end.
